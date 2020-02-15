package com.wessles.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import com.badlogic.gdx.utils.Base64Coder;

public class Validation {

	public static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkDpUgJkPu0nQbVXGWhABMsM2zRjxytJe0gHN0leoPxDZuvsWGMBbzj0Zkr+bWsfc58Ioil3pAHOF/RqhK0iXGoFcdCBIO9xqgCaXKveJB0Wxnz65geDk0c6kKgqOV9IX7/qLmW9Mc9gulf/y+/HJNnbul/StIBfwRDcwihhABJAxAikv11tR/zGy44iNyhm3Z3nEQKmUbGlcLw7nyKIGcdLL3h/yCaGS33Vcoe1oUyD3crY6FpDB3s5BIzYnVO5DbzqQggXHRttNIIfSoODucVYmsDXxH509Wce+zIXR21Yq0813TKnZ2JIurJk3h3JFIrrzPxwBj6B+DmUWKbqbbwIDAQAB";

	// Validate a source by providing a Base64 encoded encrypted hash
	public static boolean validate(String signature /* Format: Base64 */, String leveldata /* Format: UTF-8 */) {
		try {
			Signature sign = Signature.getInstance("SHA512withRSA");
			sign.initVerify(KeyUtil.getPublicKey(publicKey));
			sign.update(leveldata.getBytes());
			return sign.verify(Base64Coder.decode(signature));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String createSignature(String encryptedKey, String leveldata) {
		try {
			Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initSign(KeyUtil.getPrivateKey(encryptedKey));
			signature.update(leveldata.getBytes());
			return new String(Base64Coder.encode(signature.sign()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}
}
