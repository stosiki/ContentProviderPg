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

    // + Undo Delete line doesn't really work
    // + Add some indication that file was saved
    // + In general, in ReportActivity add error handling and user notifications of
    //   all IO operations (storage, dir creation, file writing, etc.)
    // +~ Add input verification and error indications to all user input
    //   (event line name - check for duplicates too), string input, number input
    // - CSV export
    // - Value Markers
    //   - there's a thing with value markers (Annotations, actually). Problem is that they won't (necessarily) fit
    //     onto domain axis. Possible solution might be to:
    //      - drop them altogether
    //      - never combine String type with any other type and show it on a vertical timeline chart
    //        (will also require some calculations and will probably look very weird for marginal
    //        cases when both short periods like minutes and long ones, like days should be visualized
    // - different chart types together
    //    - string (valuemarkers/valueticks)
    //    - numeric XY plot
    //    - basic (scattered plot X=const)
    // + aggregated by day
    // - (auto? with override?) individual background/text colors for eventlines
    // -- small charts
    //


// Also check out Google Analysis:
    // https://support.google.com/docs/answer/6280499?p=explore_sheets&rd=1

    // simple, simple
    // simple, string
    // string, string
    // integer, integer
    // integer, simple
    // integer, string
}
