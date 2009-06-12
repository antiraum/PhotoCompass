package de.fraunhofer.fit.photocompass.model;

/**
 * Callback interface for application model changes
 */
interface IApplicationModelCallback {

	/**
	 * Called when the minimum distance has changed.
	 * @param minDistance 	 Minimum distance in meters.
	 * @param minDistanceRel Relative minimum distance (0..1).
	 */
    void onMinDistanceChange(float minDistance, float minDistanceRel);

	/**
	 * Called when the maximum distance has changed.
	 * @param maxDistance 	 Maximum distance in meters.
	 * @param maxDistanceRel Relative maximum distance (0..1).
	 */
    void onMaxDistanceChange(float maxDistance, float maxDistanceRel);

	/**
	 * Called when the minimum age has changed.
	 * @param minAge    Minimum age in milliseconds.
	 * @param minAgeRel Relative minimum age (0..1).
	 */
    void onMinAgeChange(long minAge, float minAgeRel);

	/**
	 * Called when the maximum age has changed.
	 * @param maxAge Maximum age in milliseconds.
	 * @param maxAgeRel Relative maximum age (0..1).
	 */
    void onMaxAgeChange(long maxAge, float maxAgeRel);
}