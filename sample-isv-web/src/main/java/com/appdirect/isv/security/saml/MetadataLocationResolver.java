package com.appdirect.isv.security.saml;

/**
 * This interface maps entity ID to metadata location.
 * Implementors do this in an application specific manner.
 *
 * @author enrique.cambio
 */
public interface MetadataLocationResolver {
	/**
	 * Obtains the metadata location for the given entity ID.
	 *
	 * @param entityId The entity ID.
	 * @return the metadata location or null if unknown.
	 */
	String resolve(String entityId);
}
