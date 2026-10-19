package br.com.gmhsupport.Bcrypt;


import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {
    @Test
    public void bcryptSenha(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("andreluiz1");
        System.out.println(hash);
    }
}
