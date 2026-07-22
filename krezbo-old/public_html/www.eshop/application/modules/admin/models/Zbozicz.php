<?php
class Admin_Model_Zbozicz
{
	public function build($products)
	{
		include_once("Export/zbozicz.php");
		
		$container = new Zbozicz();

		for ( $i=0; $i < count( $products ); $i++ )
		{
			$value = $products[ $i ];

			$product = new zbozicz_item(
				$value['product'],
				$value['description'],
				$value['url'],
				$value['imgUrl'], // optional
				$value['price'], // optional
				$value['vat'], // optional
				$value['priceVat'], // optional
				$value['dues'],
				$value['deliveryDate'], // optional
				$value['itemType'], // optional
				$value['manufacturer'], // optional
				$value['categoryText'] // optional
			);

			$container->add_item( $product );
		}

		//header( "Content-type: application/xml; charset=\"".$container->charset . "\"", true );
		//header( 'Pragma: no-cache' );

		//print $container->build();
		
		$fileName = "data/exports/zbozicz.xml";
		$fileHandle = fopen($fileName, 'w') or die("can't open file");
		fwrite($fileHandle, $container->build());
		fclose($fileHandle);
	}
}

class zbozicz_item
{
	function zbozicz_item($product, $description, $url, $imgUrl = '', $price = '', $vat = '', $priceVat = '', $dues, $deliveryDate = '', $itemType = '', $manufacturer = '', $categoryText = '')
	{
		$this->product		=	$product;
		$this->description	=	$description;
		$this->url			=	$url;
		$this->imgUrl		=	$imgUrl;
		$this->price		=	$price;
		$this->vat			=	$vat;
		$this->priceVat		=	$priceVat;
		$this->dues			=	$dues;
		$this->deliveryDate	=	$deliveryDate;
		$this->itemType		=	$itemType;
		$this->manufacturer	=	$manufacturer;
		$this->categoryText	=	$categoryText;
	}
}