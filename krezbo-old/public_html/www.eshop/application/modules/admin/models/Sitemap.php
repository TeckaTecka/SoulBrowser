<?php
class Admin_Model_Sitemap
{
	public function build($products)
	{
		include_once("Export/sitemap.php");
		
		$container = new Sitemap();

		for ( $i=0; $i < count( $products ); $i++ )
		{
			$value = $products[ $i ];

			$product = new sitemap_item(
				$value['loc'],
				$value['lastmod'], // optional
				$value['changefreq'], // optional
				$value['priority'] // optional
			);

			$container->add_item( $product );
		}

		//header( "Content-type: application/xml; charset=\"".$container->charset . "\"", true );
		//header( 'Pragma: no-cache' );

		//print $container->build();
		
		$fileName = "data/exports/sitemap.xml";
		$fileHandle = fopen($fileName, 'w') or die("can't open file");
		fwrite($fileHandle, $container->build());
		fclose($fileHandle);
	}
}

class sitemap_item
{
	function sitemap_item($loc, $lastmod = '', $changefreq = '', $priority = '')
	{
		$this->loc			=	$loc;
		$this->lastmod		=	$lastmod;
		$this->changefreq	=	$changefreq;
		$this->priority		=	$priority;
	}
}