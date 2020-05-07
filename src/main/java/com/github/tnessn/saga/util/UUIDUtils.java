package com.github.tnessn.saga.util;

import java.util.UUID;

/**
 * @author huangjinfeng
 */
public class UUIDUtils {
	
	/**
	 * 生成32为uuid.
	 *
	 * @return 32位uuid
	 */
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}
	
public static void main(String[] args) {
	System.out.println(get32UUID());
}
	
}

