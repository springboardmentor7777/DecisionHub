import { useEffect, useMemo, useState } from "react";
import { AuthContext } from "./authContext.js";
import {
  ApiError,
  getCurrentUser,
  login as loginRequest,
  register as registerRequest,
} from "../services/authApi.js";

const TOKEN_KEY = "decisionhub.accessToken";

function saveAuthentication(authentication, setToken, setUser, setStatus) {
  if (!authentication?.token) {
    throw new Error("The authentication response did not contain a token.");
  }

  sessionStorage.setItem(TOKEN_KEY, authentication.token);
  setToken(authentication.token);
  setUser(authentication);
  setStatus("authenticated");
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(null);
  const [status, setStatus] = useState(() =>
    sessionStorage.getItem(TOKEN_KEY) ? "loading" : "anonymous",
  );
  const [authError, setAuthError] = useState("");

  useEffect(() => {
    if (!token || user) {
      return undefined;
    }

    let active = true;
    getCurrentUser(token)
      .then((profile) => {
        if (active) {
          setUser(profile);
          setStatus("authenticated");
          setAuthError("");
        }
      })
      .catch((error) => {
        if (!active) {
          return;
        }

        if (error instanceof ApiError && [401, 403].includes(error.status)) {
          sessionStorage.removeItem(TOKEN_KEY);
          setToken(null);
          setStatus("anonymous");
          setAuthError("Your saved session expired. Please sign in again.");
          return;
        }

        setStatus("error");
        setAuthError(error.message);
      });

    return () => {
      active = false;
    };
  }, [token, user]);

  async function signIn(credentials) {
    const authentication = await loginRequest(credentials);
    saveAuthentication(authentication, setToken, setUser, setStatus);
    setAuthError("");
  }

  async function signUp(details) {
    const authentication = await registerRequest(details);
    saveAuthentication(authentication, setToken, setUser, setStatus);
    setAuthError("");
  }

  function signOut() {
    sessionStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
    setStatus("anonymous");
    setAuthError("");
  }

  const value = useMemo(
    () => ({ authError, signIn, signOut, signUp, status, token, user }),
    [authError, status, token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
