            boolean passwordMatch = false;
            
            // 관리자 특별 처리 (평문)
            if ("oicrcutie@gmail.com".equals(user.getEmail()) && "aa667788!!".equals(request.getPassword())) {
                passwordMatch = true;
                System.out.println("Admin login with plain text password");
            } else if (user.getPassword().startsWith("$2a$")) {
                System.out.println("Using BCrypt comparison");
                passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
            } else {
                System.out.println("Using plain text comparison");
                passwordMatch = user.getPassword().equals(request.getPassword());
            }
