public boolean isTokenValid(String token) {
    try {
        return !isTokenExpired(token);
    } catch (Exception e) {
        return false;
    }
}
