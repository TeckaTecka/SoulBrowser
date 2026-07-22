<?php
class Zbozicz
{
	var $header = "<\x3Fxml version=\"1.0\" encoding=\"utf-8\"\x3F>\n<SHOP>";
	var $charset = "utf-8";
	var $footer = "</SHOP>\n";
	var $items = array();
	
	function add_item($new_item){
		if(!is_a($new_item, "zbozicz_item")){
			trigger_error("Can't add a zbozicz_item object to the zbozicz items array");
		}
		$this->items[] = $new_item;
	}
	
	function build( $file_name = null )
	{
		$map = $this->header . "\n";

		foreach($this->items as $item)
		{
			//$item->product = htmlentities($item->product, ENT_QUOTES, 'UTF-8');
			
			$map .= "\t<SHOPITEM>\n";
			
			// PRODUCT
			$map .= "\t\t<PRODUCT>$item->product</PRODUCT>\n";
			// DESCRIPTION
			$map .= "\t\t<DESCRIPTION>$item->description</DESCRIPTION>\n";
			// URL
			$map .= "\t\t<URL>$item->url</URL>\n";
			// IMGURL optional
			if ( !empty( $item->imgUrl ) )
				$map .= "\t\t<IMGURL>$item->imgUrl</IMGURL>\n";
			// PRICE optional
			if ( !empty( $item->price ) )
				$map .= "\t\t<PRICE>$item->price</PRICE>\n";
			// VAT optional
			if ( !empty( $item->vat ) )
				$map .= "\t\t<VAT>$item->vat</VAT>\n";
			// PRICE_VAT optional
			if ( !empty( $item->priceVat ) )
				$map .= "\t\t<PRICE_VAT>$item->priceVat</PRICE_VAT>\n";
			// DUES
			$map .= "\t\t<DUES>$item->dues</DUES>\n";
			// DELIVERY_DATE optional
			if ( !empty( $item->deliveryDate ) )
				$map .= "\t\t<DELIVERY_DATE>$item->deliveryDate</DELIVERY_DATE>\n";
			// ITEM_TYPE optional
			if ( !empty( $item->itemType ) )
				$map .= "\t\t<ITEM_TYPE>$item->itemType</ITEM_TYPE>\n";
			// MANUFACTURER optional
			if ( !empty( $item->manufacturer ) )
				$map .= "\t\t<MANUFACTURER>$item->manufacturer</MANUFACTURER>\n";	
			// CATEGORYTEXT optional
			if ( !empty( $item->categoryText ) )
				$map .= "\t\t<CATEGORYTEXT>$item->categoryText</CATEGORYTEXT>\n";
				
			$map .= "\t</SHOPITEM>\n";
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