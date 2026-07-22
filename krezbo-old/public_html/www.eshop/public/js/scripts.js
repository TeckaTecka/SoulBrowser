/**
 * 
 */

function addBookmark(title, url) {
        if (window.sidebar) { // firefox
              window.sidebar.addPanel(title, url,"");
        } else if( document.all ) { //MSIE
                window.external.AddFavorite( url, title);
        } else {
               alert("Omlouváme se, ale Váš prohlížeč tuto funkci nepodporuje.");
        }
}
