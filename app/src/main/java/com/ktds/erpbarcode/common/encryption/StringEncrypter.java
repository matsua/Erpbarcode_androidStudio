/**
 * Description :
 * 
 *   This file contains a class which converts a UTF-8 string into a cipher string, and vice versa.
 *   The class uses 128-bit AES Algorithm in Cipher Block Chaining (CBC) mode with a UTF-8 key
 *   string and a UTF-8 initial vector string which are hashed by MD5. PKCS5Padding is used
 *   as a padding mode and binary output is encoded by Base64. 
 * 
 * Since :
 * 
 *   2007.10.20
 * 
 * Author :
 * 
 *   JO Hyeong-ryeol (http://www.hyeongryeol.com/6)
 * 
 * Copyright :
 * 
 *   Permission to copy, use, modify, sell and distribute this software is granted provided this
 *   copyright notice appears in all copies. This software is provided "as is" without express
 *   or implied warranty, and with no claim as to its suitability for any purpose.
 *   
 *   Copyright (C) 2007 by JO Hyeong-ryeol.
 * 
 * $Id: StringEncrypter.java 65 2007-12-14 15:29:49Z JO Hyeong-ryeol $
 * 
 */
package com.ktds.erpbarcode.common.encryption;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

import com.ktds.erpbarcode.common.ErpBarcodeException;

/**
 * This class converts a UTF-8 string into a cipher string, and vice versa.
 * It uses 128-bit AES Algorithm in Cipher Block Chaining (CBC) mode with a UTF-8 key
 * string and a UTF-8 initial vector string which are hashed by MD5. PKCS5Padding is used
 * as a padding mode and binary output is encoded by Base64.
 * 
 * @author JO Hyeong-ryeol
 */
public class StringEncrypter {
	private static final String TAG = "StringEncrypter";
	
	private Cipher rijndael;
	private SecretKeySpec key;
	private IvParameterSpec initalVector;

	/**
	 * Creates a StringEncrypter instance.
	 * 
	 * @param key A key string which is converted into UTF-8 and hashed by MD5.
	 *            Null or an empty string is not allowed.
	 * @param initialVector An initial vector string which is converted into UTF-8
	 *                      and hashed by MD5. Null or an empty string is not allowed.
	 * @throws Exception
	 */
	public StringEncrypter(String key, String initialVector) throws ErpBarcodeException {
		if (key.isEmpty()) {
			Log.i(TAG, "암화화 Key정보가 없습니다.");
			throw new ErpBarcodeException(-1, "암화화 Key정보가 없습니다. ");
		}
		if (initialVector.isEmpty()) {
			Log.i(TAG, "초기화 벡터 정보가 없습니다.");
			throw new ErpBarcodeException(-1, "초기화 벡터 정보가 없습니다. ");
		}

		
		try {
			// Create a AES algorithm.
			//this.rijndael = Cipher.getInstance("AES/CBC/PKCS7Padding");
			this.rijndael = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			// Initialize an encryption key and an initial vector.
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			
			this.key = new SecretKeySpec(md5.digest(key.getBytes("UTF8")), "AES");
			this.initalVector = new IvParameterSpec(md5.digest(initialVector.getBytes("UTF8")));
		} catch (NoSuchAlgorithmException e) {
			Log.i(TAG, "존재하지 않는 암호화 알고리즘입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "존재하지 않는 암호화 알고리즘입니다. ");
		} catch (NoSuchPaddingException e) {
			Log.i(TAG, "존재하지 않는 패딩 정보입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "존재하지 않는 패딩 정보입니다. ");
		} catch (UnsupportedEncodingException e) {
			Log.i(TAG, "지원되지 않는 엔코딩정보 입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "지원되지 않는 엔코딩정보 입니다. ");
		} catch (Exception e) {
			Log.i(TAG, "암호화키 초기화중 오류가 발생하였습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "암호화키 초기화중 오류가 발생하였습니다. ");
		}
		
	}

	/**
	 * Encrypts a string.
	 * 
	 * @param value A string to encrypt. It is converted into UTF-8 before being encrypted.
	 *              Null is regarded as an empty string.
	 * @return An encrypted string.
	 * @throws ErpBarcodeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws Exception
	 */
	public String encrypt(String message) throws ErpBarcodeException  {
		if (message.isEmpty()) {
			return "";
		}

		byte[] encryptedBytes;
		try {
			// Initialize the cryptography algorithm.
			this.rijndael.init(Cipher.ENCRYPT_MODE, this.key, this.initalVector);
			// Get a UTF-8 byte array from a unicode string.
			byte[] utf8Value = message.getBytes(Charset.defaultCharset());

			// Encrypt the UTF-8 byte array.
			encryptedBytes = this.rijndael.doFinal(utf8Value);
		} catch (InvalidKeyException e) {
			Log.i(TAG, "잘못된 Key정보입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 Key정보입니다. ");
		} catch (InvalidAlgorithmParameterException e) {
			Log.i(TAG, "잘못된 암호화 알고리즘 정보입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 암호화 알고리즘 정보입니다. ");
		} catch (IllegalBlockSizeException e) {
			Log.i(TAG, "잘못된 블럭크기입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 블럭크기입니다. ");
		} catch (BadPaddingException e) {
			Log.i(TAG, "잘못된 블럭크기입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 블럭크기입니다. ");
		} catch (Exception e) {
			Log.i(TAG, "암호화중 오류가 발생하였습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "암호화중 오류가 발생하였습니다. ");
		}

		// Return a base64 encoded string of the encrypted byte array.
		//return Base64.encodeToString(encryptedValue, 0);
		return Base64Encoder.encode(encryptedBytes);
	}

	/**
	 * Decrypts a string which is encrypted with the same key and initial vector. 
	 * 
	 * @param value A string to decrypt. It must be a string encrypted with the same key and initial vector.
	 *              Null or an empty string is not allowed.
	 * @return A decrypted string
	 * @throws ErpBarcodeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 */
	public String decrypt(String encrypted) throws ErpBarcodeException {
		
		if (encrypted.isEmpty()) {
			Log.i(TAG, "복호화 메시지정보가 없습니다.");
			throw new ErpBarcodeException(-1, "복호화 메시지정보가 없습니다. ");
		}

		byte[] originalBytes = null;
		try {
			// Initialize the cryptography algorithm.
			this.rijndael.init(Cipher.DECRYPT_MODE, this.key, this.initalVector);
			
			// Get an encrypted byte array from a base64 encoded string.
			byte[] encryptedBytes = Base64.decode(encrypted, 0);

			// Decrypt the byte array.
			originalBytes = this.rijndael.doFinal(encryptedBytes);

		} catch (InvalidKeyException e) {
			Log.i(TAG, "잘못된 Key정보입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 Key정보입니다. ");
		} catch (InvalidAlgorithmParameterException e) {
			Log.i(TAG, "잘못된 암호화 알고리즘 정보입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 암호화 알고리즘 정보입니다. ");
		} catch (IllegalBlockSizeException e) {
			Log.i(TAG, "잘못된 블럭크기입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 블럭크기입니다. ");
		} catch (BadPaddingException e) {
			Log.i(TAG, "잘못된 블럭크기입니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "잘못된 블럭크기입니다. ");
		} catch (Exception e) {
			Log.i(TAG, "복호화중 오류가 발생하였습니다." + e.getMessage());
			throw new ErpBarcodeException(-1, "복호화중 오류가 발생하였습니다. ");
		}
		
		String originalString = "";
		try {
			originalString = new String(originalBytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			Log.i(TAG, "바이너리(UTF-8) 문자로 엔코딩중 오류가 발생햇습니다. " + e.getMessage());
			throw new ErpBarcodeException(-1, "바이너리 문자 엔코딩중 오류가 발생했습니다. ");
		}

		// Return a string converted from the UTF-8 byte array.
		return originalString;
	}

}
