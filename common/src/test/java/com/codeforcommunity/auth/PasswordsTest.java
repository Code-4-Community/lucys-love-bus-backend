package com.codeforcommunity.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

// From Brandon: tests look good so I'm leaving alone for now
class PasswordsTest {
  private final String pass = "Password";

  @Test
  void testHashLength() {
    byte[] hash = Passwords.createHash(pass);
    assertEquals(Passwords.KEY_LENGTH / 8 + Passwords.SALT_LENGTH, hash.length);
  }

  @Test
  void testHashSaltRandomized() {
    byte[] hash1 = Passwords.createHash(pass);
    byte[] hash2 = Passwords.createHash(pass);
    assertEquals(hash1.length, hash2.length);
    assertNotEquals(Arrays.toString(hash1), Arrays.toString(hash2));
  }

  @Test
  void testIsExpectedPasswordMatchesSamePasses() {
    byte[] hash = Passwords.createHash(pass);
    assertTrue(Passwords.isExpectedPassword(pass, hash));
  }

  @Test
  void testIsExpectedPasswordDeniesDifferentPasses() {
    byte[] hash = Passwords.createHash(pass);
    assertFalse(Passwords.isExpectedPassword("password", hash));
    assertFalse(Passwords.isExpectedPassword("", hash));
    assertFalse(Passwords.isExpectedPassword("PASSWORD", hash));
  }

  @Test
  void testGenerateRandomTokenLength() {
    String token = Passwords.generateRandomToken(1);
    assertEquals(1, token.length());

    token = Passwords.generateRandomToken(5);
    assertEquals(5, token.length());

    token = Passwords.generateRandomToken(256);
    assertEquals(256, token.length());
  }

  @Test
  void testGenerateRandomTokenRandomness() {
    int len = 10;
    String[] tokens = new String[len];
    for (int i = 0; i < len; i++) {
      tokens[i] = Passwords.generateRandomToken(10);
    }

    for (int i = 0; i < len; i++) {
      for (int j = 0; j < len; j++) {
        if (i != j) {
          assertNotEquals(tokens[i], tokens[j]);
        }
      }
    }
  }
}
