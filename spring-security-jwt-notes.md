# Spring Security & JWT Notes

---

## Spring Security

### Core Requirements of a Backend Application

When developing a backend application, there are four essential aspects to consider.

#### 1. The Application Must Work
The most basic requirement is that the application functions correctly. Endpoints should return the expected responses. The request must be processed correctly and the appropriate response should be returned.

#### 2. Stability
The application must remain stable and should not crash due to invalid input or unexpected errors. For this reason, backend applications implement exception handling to maintain stability.

#### 3. Performance
Applications should respond quickly and efficiently. Important performance considerations include fast API response time, optimized database queries, and caching strategies. Users expect modern applications to respond instantly.

#### 4. Security
Security is one of the most critical aspects of backend development. Examples of security concerns include login authentication, authorization control, protection of sensitive data, and prevention of attacks. If sensitive data is leaked, it may cause severe consequences for users and organizations.

---

### What Spring Security Does

Spring Security is a framework that provides security features for Java applications.

| Feature | Purpose |
|---|---|
| Authentication | Verifies user identity |
| Authorization | Controls access to resources |
| CSRF Protection | Prevents cross-site attacks |
| Session Security | Manages user sessions securely |
| Password Hashing | Secure password storage |

---

### Spring Security Architecture

In a standard Spring MVC application, requests follow this flow:

```
Client → DispatcherServlet → Controller
```

When Spring Security is added, an additional security layer is introduced:

```
Client
↓
Security Filter Chain
↓
DispatcherServlet
↓
Controller
```

All incoming requests pass through the Security Filter Chain before reaching the application. This allows Spring Security to perform authentication and authorization checks.

---

### Security Filter Chain

Spring Security works by applying multiple filters to incoming requests. Examples of filters include `CsrfFilter`, `AuthenticationFilter`, `LogoutFilter`, and `AuthorizationFilter`.

```
Client Request
↓
Filter 1
↓
Filter 2
↓
Filter 3
↓
Controller
```

Each filter performs a specific security-related task.

#### Why We Use addFilterBefore
Spring Security has its own default filters. When using JWT, we need our `JwtFilter` to run before Spring's `UsernamePasswordAuthenticationFilter`. This way, every request passes through our token check first. If we didn't do this, Spring Security would block the request before our filter even runs.

```java
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
```

---

### What Happens When Spring Security Dependency Is Added

Adding `spring-boot-starter-security` enables Spring Security automatically. Once added, Spring Boot automatically configures login page, authentication, logout functionality, session management, and security filters — even if no security code is written.

#### Default Login Credentials
Spring Security provides default credentials when the application starts.
- Default username: `user`
- Default password: a randomly generated password that appears in the console logs, changes each restart

---

### Session Mechanism

When a user logs in successfully, the server creates a session stored in a cookie called `JSESSIONID`. Subsequent requests include this cookie and the server verifies the session without requiring login again.

---

### CSRF Protection

CSRF stands for Cross Site Request Forgery — a type of attack where a malicious website sends a request to another website using a valid user session.

**Example scenario:**
1. A user is logged into a banking application
2. An attacker tricks the user into visiting a malicious website
3. The malicious site sends `POST /transfer`
4. If the session cookie is automatically included, the request may be processed as legitimate

To prevent this, Spring Security uses CSRF Tokens — each request must include a valid token.

#### Why We Disable CSRF When Using JWT
CSRF protection is designed for session-based systems where the browser automatically sends cookies. With JWT, there are no cookies — every request carries a token in the Authorization header. An attacker cannot access this token, so CSRF attacks are not possible.

---

### Stateful vs Stateless Authentication

| | STATEFUL | STATELESS |
|---|---|---|
| Storage | Server stores session | Server stores nothing |
| Client sends | JSESSIONID cookie | JWT token in header |
| Multiple servers | Problem | No problem |
| Use case | Traditional web apps | REST API + JWT |

#### What STATELESS Does in Spring Security
By setting `SessionCreationPolicy.STATELESS`, we tell Spring Security to never create a session. Every request must carry its own authentication info — in our case, a JWT token. Once a request is done, the server forgets the user completely.

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

#### What STATEFUL Does in Spring Security
By default, Spring Security is stateful. After a successful login, it creates a session and stores it on the server. The client receives a JSESSIONID cookie and sends it with every request. This works fine with a single server but breaks with multiple servers because only one server has the session.

---

### Spring Security Configuration

Security configuration is typically defined using a `SecurityFilterChain`.

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
   http
       .csrf(csrf -> csrf.disable())
       .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
       .httpBasic(Customizer.withDefaults());
   return http.build();
}
```

---

### Hardcoded Users

Before integrating a database, users can be stored in memory using `InMemoryUserDetailsManager`. This is not production ready because passwords may be stored in plain text, user data disappears when the application restarts, and users cannot be managed dynamically.

---

### Core Components of Spring Security

#### 1. UserDetails
`UserDetails` is an interface provided by Spring Security. It represents the user in a format that Spring Security understands. Our own `User` entity does not mean anything to Spring Security by default. By implementing `UserDetails` in our `User` class, we tell Spring Security "this is a user, treat it as one."

Key methods:
- `getUsername()` → returns the username
- `getPassword()` → returns the hashed password
- `getAuthorities()` → returns the roles/permissions of the user
- `isAccountNonExpired()`, `isAccountNonLocked()`, `isEnabled()` → account status checks

#### 2. UserDetailsService
`UserDetailsService` is an interface with a single method: `loadUserByUsername()`. Spring Security calls this method when a user tries to log in. We implement this interface and write the logic to fetch the user from the database. If the user is not found, we throw a `UsernameNotFoundException`. Without `UserDetailsService`, Spring Security does not know how to find the user.

#### 3. PasswordEncoder
Passwords must never be stored as plain text in the database. `PasswordEncoder` hashes the password before saving it. During login, Spring Security hashes the incoming password and compares it with the hashed password in the database. We use `BCryptPasswordEncoder` which is the most common implementation.

It is defined as a separate bean because it may be needed in multiple places, such as during registration. Spring manages one instance and injects it wherever needed.

#### 4. DaoAuthenticationProvider
`DaoAuthenticationProvider` is an implementation of `AuthenticationProvider`. DAO stands for Data Access Object, meaning it goes to the database to verify the user.

Steps it follows:
1. Takes the username from the login request
2. Calls `UserDetailsService` to fetch the user from the database
3. Uses `PasswordEncoder` to compare the passwords
4. If everything matches, authentication is successful

It needs both `UserDetailsService` and `PasswordEncoder` to do its job.

#### 5. AuthenticationProvider
`AuthenticationProvider` is an interface. Other implementations exist such as `LdapAuthenticationProvider` for LDAP authentication. We define it as a bean using the interface type, not the implementation type. This follows the principle: program to an interface, not an implementation. Tomorrow if we want to switch to a different provider, only the inside changes.

#### 6. AuthenticationManager
`AuthenticationManager` is the mechanism that starts the authentication process. Its only purpose is to say "let's start the process" and decide which `AuthenticationProvider` will handle the request. In large applications there can be multiple `AuthenticationProvider`s and `AuthenticationManager` decides which one to use.

#### 7. AuthenticationConfiguration
Spring Boot internally creates an `AuthenticationManager` automatically but it is not directly injectable. `AuthenticationConfiguration` exposes Spring's internally created `AuthenticationManager` as a bean so we can inject it wherever we need it, such as in `AuthService`.

#### 8. SecurityFilterChain
`SecurityFilterChain` is where we define the security rules of our application — which endpoints are public, which require authentication, which require specific roles, session management, and CSRF configuration.

```java
.requestMatchers("/auth/**").permitAll()      // everyone can access
.anyRequest().authenticated()                 // everything else requires login
```

#### 9. SecurityContext and SecurityContextHolder
`SecurityContext` is the place where Spring Security keeps the answer to "who is currently authenticated for this request?" `SecurityContextHolder` is the class that holds the `SecurityContext`. It works on a per-thread basis — every request runs in its own thread and has its own `SecurityContext`. This means 1000 users making requests at the same time will not interfere with each other.

**In stateless applications:**
- Every request starts with an empty `SecurityContext`
- `JwtFilter` runs, validates the token and puts the user into `SecurityContext`
- The request reaches the controller and is processed
- When the request is done, `SecurityContext` is cleared
- The next request starts fresh again

**In stateful applications:**
- After login, `SecurityContext` is saved into the session
- Every subsequent request loads it from the session
- It persists until the user logs out

#### 10. Role-Based Authorization
Role-based authorization controls what an authenticated user can access.

```java
.requestMatchers("/admin/**").hasRole("ADMIN")   // only ADMIN can access
.requestMatchers("/user/**").hasRole("USER")      // only USER can access
```

`hasRole("ADMIN")` internally looks for `ROLE_ADMIN` in the user's authorities. That's why we name our roles `ROLE_USER` and `ROLE_ADMIN` — Spring expects this prefix.

We store roles as an enum to prevent typos and invalid values. `@Enumerated(EnumType.STRING)` stores the enum as a string in the database instead of a number, which is safer because adding new enum values won't break existing data.

---

## JWT

### Cryptography Basics

#### The Problem
When A sends data to B over the internet, C can intercept it, read it, or modify it. This is called a man-in-the-middle attack.

#### Symmetric Encryption
- A and B use the same key to encrypt and decrypt
- Fast and supports large key sizes
- Common algorithms: AES, DES
- **Problem:** the key must be shared before communication — can't share it over the internet because C would see it
- **Problem:** if there are multiple people in the network, you need a separate key for every pair → hard to manage

#### Asymmetric Encryption
- Everyone has two keys: a public key (known by everyone) and a private key (known only by the owner)
- To send a message to B, A encrypts it with B's public key
- Only B can decrypt it using B's private key
- C can intercept the message but cannot decrypt it
- Common algorithms: RSA, ECC

#### Symmetric vs Asymmetric

| | Symmetric | Asymmetric |
|---|---|---|
| Keys | Same key for both sides | Public + private key pair |
| Speed | Faster | Slower |
| Key sharing | Must share beforehand | No need |
| Scalability | Hard to manage with many users | Easy |
| Algorithms | AES, DES | RSA, ECC |

#### The Identity Problem
Asymmetric encryption provides security but not identity verification. C can encrypt a fake message with B's public key and pretend to be A. B has no way to tell the difference.

#### Digital Signature
- A encrypts the message with A's own private key
- B decrypts it using A's public key
- If decryption succeeds → proof that A sent it
- If C tries to fake it → C encrypts with C's private key → B can't decrypt with A's public key → B knows something is wrong
- **Problem:** anyone can decrypt with A's public key and read the message — no confidentiality

#### Double Encryption (Signature + Security)
To achieve both identity and security at the same time:
1. A encrypts the message with B's public key → only B can read it
2. A encrypts again with A's private key → proves A sent it

When B receives it:
- B decrypts the outer layer with A's public key → identity verified
- B decrypts the inner layer with B's private key → reads the message

If C intercepts: C can open the outer layer with A's public key but cannot open the inner layer — doesn't know B's private key.

---

### JWT (JSON Web Tokens)

#### The Problem with Sessions
When a user logs in, the server creates a session and stores it. The client gets a session ID via cookie and sends it with every request. This works fine with one server, but breaks with horizontal scaling — if you have multiple servers, only one of them has the session. Workarounds exist (shared database, sticky sessions via load balancer) but they add complexity.

#### The JWT Solution
Instead of storing session data on the server, the server gives the client a signed token that contains all necessary information. The client stores it and sends it with every request. Any server can verify the token without needing shared state. This makes JWT stateless — the server stores nothing.

---

### JWT Structure

#### 1. Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
Specifies the algorithm used to sign the token.

#### 2. Payload
```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022,
  "exp": 1516240822
}
```
Contains the actual data — who the user is, when the token was issued, when it expires. Keep the payload as small as possible. Never put sensitive data here because the payload is not encrypted by default — anyone can read it.

#### 3. Signature
Created by signing the header + payload with a secret key. If anyone modifies the payload, the signature breaks and the server rejects the token.

---

### JWT is Signed, Not Encrypted by Default
- Anyone can read the payload
- Nobody can modify it without breaking the signature
- Encryption is possible but not enabled by default

---

### How JWT Works in Practice
1. Client sends username and password to the server
2. Server verifies credentials and generates a JWT token
3. Server sends the token to the client
4. Client stores the token
5. On every subsequent request, client sends the token in the Authorization header
6. Server verifies the token and returns the requested resource
7. If the token is expired or invalid, server rejects the request

---

### Where Should the Client Store the Token?
- **LocalStorage** → easy but vulnerable to XSS attacks ❌
- **HttpOnly Cookie** → JavaScript cannot read it, safe from XSS but needs CSRF protection ✅
- **Memory** → safest but lost on page refresh

Best practice: HttpOnly Cookie + CSRF token for web apps.

---

### Why "Bearer"?
Bearer comes from the OAuth2 standard and means "whoever carries this token has access." It tells the server this is JWT authentication, not Basic Auth or another scheme.

```
Authorization: Bearer eyJhbGci...
```

---

### Session vs JWT

| | Session | JWT |
|---|---|---|
| Storage | Server-side | Client-side |
| Scalability | Poor (stateful) | Good (stateless) |
| Multiple servers | Needs shared DB or sticky sessions | Works out of the box |
| Revocation | Easy | Harder (need blacklist) |

---

### Refresh Token

Access token has a short lifespan (15 min - 1 hour) for security. Refresh token's only job is to get a new access token when it expires. It is long-lived (7-30 days) and stored in the database.

```
Access token expires
↓
Client sends refresh token to /auth/refresh
↓
Server checks refresh token in DB
↓
Issues new access token
↓
User never has to log in again until refresh token expires
```

Logout → refresh token deleted from DB → no new access token can be issued.

Refresh token does not need to carry any information inside. Its only job is to exist in the database and prove the user logged in before. A simple UUID is enough for this. Using JWT would add unnecessary complexity.

---

### Algorithms

- **HS256 (HMAC SHA-256)** — symmetric, one secret key shared between both sides
- **RS256 (RSA)** — asymmetric, server signs with private key, verifies with public key
- Higher the number → larger key size → more secure

---

### Disadvantages of JWT
- Cannot be revoked easily → stolen token stays valid until expiration, need a blacklist to invalidate early
- Payload is not encrypted → never store sensitive data
- Larger size than session ID → adds overhead to every request
- If secret key is leaked → anyone can forge tokens

---

### Key Takeaways for Interview
- JWT is stateless — server stores nothing
- Token has three parts: header, payload, signature
- Payload is readable by anyone — never store sensitive data
- Signature prevents tampering, not reading
- Solves the scaling problem that session-based auth has
- Token should have an expiration time (`exp` claim)
