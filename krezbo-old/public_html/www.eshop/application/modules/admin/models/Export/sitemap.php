<?php
class sitemap
{
	var $header = "<\x3Fxml version=\"1.0\" encoding=\"utf-8\"\x3F>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";
	var $charset = "utf-8";
	var $footer = "</urlset>\n";
	var $items = array();
	
	function add_item($new_item){
		if(!is_a($new_item, "sitemap_item")){
			trigger_error("Can't add a sitemap_item object to the sitemap items array");
		}
		$this->items[] = $new_item;
	}
	
	function build( $file_name = null )
	{
		$map = $this->header . "\n";

		foreach($this->items as $item)
		{
			//$item->product = htmlentities($item->product, ENT_QUOTES, 'UTF-8');
			
			$map .= "\t<url>\n";
			
			// LOC
			$map .= "\t\t<loc>$item->loc</loc>\n";
			// LASTMOD optional
			if ( !empty( $item->lastmod ) )
				$map .= "\t\t<lastmod>$item->lastmod</lastmod>\n";
			// CHANGEFREQ optional
			if ( !empty( $item->changefreq ) )
				$map .= "\t\t<changefreq>$item->changefreq</changefreq>\n";
			// PRIORITY optional
			if ( !empty( $item->priority ) )
				$map .= "\t\t<priority>$item->priority</priority>\n";
			
			$map .= "\t</url>\n";
		}

		$map .= $this->footer;

		if(!is_null($file_name)){
			$fh = fopen($file_name, 'w');
			fwrite($fh, $map);
			fclose($fh);
		}else{
			return $map;
		}
	}
}