package study.stosiki.com.contentproviderpg;

/**
 * Created by User on 21/07/2015.
 */
public class Comments {
    // http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/

    /*** TODO ***/
    // Adding of event lines is OK
    // To the create event line dialog
    // -- add verification for non-empty event line title
    // -- add check for unique line title
    // add line types
    // override getView in list adapter to:
    // - show different line types
    // - show selected item


    // (optimization) in the getView of MainActivity implement Holder-getTag pattern

    // ? add more line types e.g. Numeric Range, Mark (same as Range, but F to A)

    // add analysis, graphs, statistics,

    // General: look for things that might be correlated in short-term
    // eg number of cups coffee and blood pressure
    // think of things that are available on the phone already and might be used
    // as series to find correlations with

    //v.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
    //
    /*
    However, some Views already have backgrounds. A Button, for example, already has a background:
    the face of the button itself. This background is a StateListDrawable, which you can find in
    android-2.1/data/res/drawable/btn_default.xml in your Android SDK installation. That, in turn,
    refers to a bunch of nine-patch bitmap images, available in multiple densities. You would need
    to clone and modify all of that to accomplish your green goals.
     */

    /** Done **/
    // remove event line list item background if another event line was short clicked
    // remove evenr line list item background after event line was deleted (currently it goes to n+1 element)


    // TODOs
    // + add tabs to the ReportActivity (EventList, Chart)
    // + make it possible to select exactly 2 eventlines
    // - if two eventlines are selected, both charts are present on the Chart tab
    //    and another 'Statistics' tab (currently not implemented) is added
    // EventNumeric and EventStringPropertyDialogFragments please use polymorphism, Bridge, Strategy
    //    or whatever but clean the mess

    // -




}
