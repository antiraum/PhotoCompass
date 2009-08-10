package de.fraunhofer.fit.photocompass.model;

/**
 * Callback interface for photos model changes
 */
interface IPhotosModelCallback {

	/**
	 * Called when the list of photo distances has changed.
	 * @param photoDistances Distances of the photos in relative values (0..1).
	 */
    void onPhotosDistancesChange(in float[] photoDistances);

	/**
	 * Called when the list of photo ages has changed.
	 * @param photoAges Ages of the photos in relative values (0..1).
	 */
    void onPhotosAgesChange(in float[] photoAges);
}