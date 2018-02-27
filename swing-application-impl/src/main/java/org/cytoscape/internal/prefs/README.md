```prefs``` includes one class for each panel in the new dialog.  

```Cy3PreferencesPanel``` is the main panel of the dialog.  Individual panels are kept in a ```CardLayout``` defined in the abstract parent ```PreferencesContainer```

The order and layout of panels is dictated by the enumeration ```PPanels```.

```Cy3PreferencesPanel``` contains the initialization of all the other panels.  

Each sub panel has its own data transfer routines called ```install()``` and ```extract()```. ```Cy3PreferencesPanel``` iterates thru its children in its own install() and extract()

There is one subdirectory called ```lib```.  It includes a couple of dozen of Swing utilities from my collection. 
These are probably more appropriately located in the ```..internal.view``` package, but for now I wanted to keep all of my additions localized in a single package.

Most of the GUI code is in ```AbstractPrefsPanel```, the class from which all the use specific panels derive: ```PrefsBehavior, PrefsPrivacy```, etc.
