package tracker.test;

/**
 * To keep the tests from messing with my real Tiny Time Tracker preferences,
 * I need a class in a new package just to have a unique moniker to hand to
 * Preferences#userNodeForPackage. So, that's all this class is for, it to
 * have a class in a package other than the 'tracker' package.
 * 
 * A note about the placement of the unit test classes: We want the UTs to 
 * have package level access to the classes they are testing, so we don't
 * want to put the UTs in this 'tracker.test' package. Some people put the
 * UTs in a parallel folder structure with the same package names for better
 * separation and easier packaging (jar-ing). We could do that, but for now,
 * it seems managable all in one folder structure.
 */
public class PlaceHolder {
}
