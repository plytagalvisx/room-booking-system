import { useState } from "react";
import keycloak from "../../auth/keycloak";

type UserInfo = {
    subject: string;
    username: string;
    email: string;
};

function AuthPage() {
    const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleLogin() {
        await keycloak.login({
            redirectUri: `${window.location.origin}/auth`,
        });
    }

    async function handleLogout() {
        await keycloak.logout({
            redirectUri: `${window.location.origin}/auth`,
        });
    }

    async function handleCallBackend() {
        setError(null);
        try {
            await keycloak.updateToken(30); // Refresh the token if it's about to expire in 30 seconds
            const response = await fetch("http://localhost:8080/api/auth/me", // we fetch the user info from the protected backend REST API endpoint. We use the backend endpoint to get private protected user info.
                {
                    headers: {
                        "Authorization": `Bearer ${keycloak.token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error(`Request failed: ${response.status}`);
            }

            const data: UserInfo = await response.json();
            setUserInfo(data);
        } catch (error) {
            if (error instanceof Error) {
                setError(error.message);
            } else {
                setError("Something went wrong.");
            }
        }
    }

    return (
        <main>
            <h1>Authentication</h1>

            {keycloak.authenticated ? (
                <>
                    <p>
                        Authenticated as:{" "}      {/* here we display the username from the token parsed by Keycloak */}
                        {
                            keycloak.tokenParsed?.preferred_username
                        }
                    </p>

                    <button
                        type="button"
                        onClick={handleCallBackend}
                    >
                        Call protected backend
                    </button>

                    <button
                        type="button"
                        onClick={handleLogout}
                    >
                        Logout
                    </button>
                </>
            ) : (
                <>
                    <p>
                        You are not authenticated.
                    </p>

                    <button
                        type="button"
                        onClick={handleLogin}
                    >
                        Login
                    </button>
                </>
            )}

            {userInfo && (
                <div>
                    <p>
                        Subject: {userInfo.subject}
                    </p>
                    <p>
                        Username: {userInfo.username}
                    </p>
                    <p>
                        Email: {userInfo.email}
                    </p>
                </div>
            )}

            {error && <p>Error: {error}</p>}
        </main>
    );
}

export default AuthPage;


// await keycloak.updateToken(30);
// Access tokens expire. This asks Keycloak's adapter to refresh the token if it has fewer than 30 seconds of validity remaining.
// The official adapter recommends refreshing tokens before protected API requests when necessary.

// Authorization: `Bearer ${keycloak.token}`
// This is the connection.
// This is the header that will be sent with the request.

// The HTTP request becomes approximately:
//      GET /api/auth/me HTTP/1.1
//      Host: localhost:8080
//      Authorization: Bearer eyJhbGciOiJSUzI1NiIs...