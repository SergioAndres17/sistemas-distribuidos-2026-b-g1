// Package authz is the proposed fix for synkro-products-api: verify the RS256
// signature before trusting the roles used by canWriteCatalog (ADR-006, STRIDE #6).
package authz

import (
	"crypto"
	"crypto/rsa"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"net/http"
	"slices"
	"strings"
	"time"
)

type claims struct {
	Sub         string   `json:"sub"`
	Roles       []string `json:"roles"`
	Permissions []string `json:"permissions"`
	Exp         int64    `json:"exp"`
}

// verify accepts only RS256 tokens signed with the Auth public key.
func verify(token string, key *rsa.PublicKey) (*claims, bool) {
	parts := strings.Split(token, ".")
	if len(parts) != 3 {
		return nil, false
	}
	header, err1 := base64.RawURLEncoding.DecodeString(parts[0])
	payload, err2 := base64.RawURLEncoding.DecodeString(parts[1])
	signature, err3 := base64.RawURLEncoding.DecodeString(parts[2])
	if err1 != nil || err2 != nil || err3 != nil {
		return nil, false
	}
	var h struct {
		Alg string `json:"alg"`
	}
	if json.Unmarshal(header, &h) != nil || h.Alg != "RS256" {
		return nil, false
	}
	digest := sha256.Sum256([]byte(parts[0] + "." + parts[1]))
	if rsa.VerifyPKCS1v15(key, crypto.SHA256, digest[:], signature) != nil {
		return nil, false
	}
	var c claims
	if json.Unmarshal(payload, &c) != nil || c.Exp < time.Now().Unix() {
		return nil, false
	}
	return &c, true
}

// canWriteCatalog is the same rule as synkro-products-api: ADMIN, INVENTORY or products:write.
func canWriteCatalog(c *claims) bool {
	return slices.Contains(c.Permissions, "products:write") ||
		slices.Contains(c.Roles, "ADMIN") || slices.Contains(c.Roles, "INVENTORY")
}

// RequireCatalogWrite answers 401 without a verified token and 403 without write rights.
func RequireCatalogWrite(key *rsa.PublicKey, next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token, found := strings.CutPrefix(r.Header.Get("Authorization"), "Bearer ")
		c, ok := verify(token, key)
		if !found || !ok {
			http.Error(w, `{"error":"UNAUTHORIZED"}`, http.StatusUnauthorized)
			return
		}
		if !canWriteCatalog(c) {
			http.Error(w, `{"error":"FORBIDDEN"}`, http.StatusForbidden)
			return
		}
		next.ServeHTTP(w, r)
	})
}
