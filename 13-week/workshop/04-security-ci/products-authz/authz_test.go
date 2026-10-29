package authz

import (
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
)

var enc = base64.RawURLEncoding

func payload(sub string, roles []string) string {
	body, _ := json.Marshal(claims{Sub: sub, Roles: roles, Exp: time.Now().Add(time.Hour).Unix()})
	return enc.EncodeToString(body)
}

func sign(t *testing.T, key *rsa.PrivateKey, sub string, roles []string) string {
	unsigned := enc.EncodeToString([]byte(`{"alg":"RS256","typ":"JWT"}`)) + "." + payload(sub, roles)
	digest := sha256.Sum256([]byte(unsigned))
	signature, err := rsa.SignPKCS1v15(rand.Reader, key, crypto.SHA256, digest[:])
	if err != nil {
		t.Fatal(err)
	}
	return unsigned + "." + enc.EncodeToString(signature)
}

// Story: only a verified ADMIN or INVENTORY token can change a product price.
func TestPriceChangeRejectsForgedAndUnauthorizedTokens(t *testing.T) {
	auth, _ := rsa.GenerateKey(rand.Reader, 2048)
	attacker, _ := rsa.GenerateKey(rand.Reader, 2048)
	updated := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) { w.WriteHeader(http.StatusOK) })
	server := httptest.NewServer(RequireCatalogWrite(&auth.PublicKey, updated))
	defer server.Close()

	noneHeader := enc.EncodeToString([]byte(`{"alg":"none","typ":"JWT"}`))
	cases := []struct {
		name  string
		token string
		want  int
	}{
		{"no token", "", http.StatusUnauthorized},
		{"edited payload with ADMIN role, alg none", noneHeader + "." + payload("salesperson-uuid", []string{"ADMIN"}) + ".x", http.StatusUnauthorized},
		{"ADMIN token signed with another key", sign(t, attacker, "salesperson-uuid", []string{"ADMIN"}), http.StatusUnauthorized},
		{"valid SALESPERSON token", sign(t, auth, "salesperson-uuid", []string{"SALESPERSON"}), http.StatusForbidden},
		{"valid INVENTORY token", sign(t, auth, "inventory-uuid", []string{"INVENTORY"}), http.StatusOK},
	}
	for _, c := range cases {
		body := strings.NewReader(`{"name":"Mouse","priceCents":1,"categoryId":"c1"}`)
		req, _ := http.NewRequest(http.MethodPut, server.URL+"/api/v1/products/p1", body)
		if c.token != "" {
			req.Header.Set("Authorization", "Bearer "+c.token)
		}
		res, err := http.DefaultClient.Do(req)
		if err != nil {
			t.Fatal(err)
		}
		res.Body.Close()
		if res.StatusCode != c.want {
			t.Errorf("%s: got %d, want %d", c.name, res.StatusCode, c.want)
		}
	}
}
