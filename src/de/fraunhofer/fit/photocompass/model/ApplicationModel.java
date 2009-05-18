package de.fraunhofer.fit.photocompass.model;

public class ApplicationModel {

    private static ApplicationModel _instance;

	private float _maxDistance; // in meters
	private int _minAge; // in ...
	private int _maxAge; // in ...
	
	protected ApplicationModel() {
		super();
		
		// default values
		_maxDistance = 100000;
		_minAge = 0;
		_maxAge = 100000;
	}

    public static ApplicationModel getInstance() {
        if (_instance == null) _instance = new ApplicationModel();
        return _instance;
    }

	public float getMaxDistance() {
		return _maxDistance;
	}

	public void setMaxDistance(float value) {
		_maxDistance = value;
	}

	public int getMinAge() {
		return _minAge;
	}

	public void setMinAge(int value) {
		_minAge = value;
	}

	public int getMaxAge() {
		return _maxAge;
	}

	public void setMaxAge(int value) {
		_maxAge = value;
	}
}
