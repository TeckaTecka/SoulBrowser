<?php
class Admin_Model_Thumb
{
	public function createThumbAdaptive($orig_image, $new_image, $width, $height) {
		require_once 'Thumb/WideImage.php';
		
		WideImage::load($orig_image)
			->resize($width, $height, 'inside', 'down')
			->resizeCanvas($width, $height, 'center', 'center', hexdec('ffffff'), 'any', true)
			->saveToFile($new_image, 80);
	}
	
	public function createThumbWatermark($orig_image, $new_image, $width, $height, $watermark) {
		require_once 'Thumb/WideImage.php';
		
		$watermark = WideImage::load($watermark);
		
		WideImage::load($orig_image)
			->resize($width, $height, 'inside', 'down')
			->resizeCanvas($width, $height, 'center', 'center', hexdec('ffffff'), 'any', true)
			->merge($watermark, 'center', 'center', 100)
			->saveToFile($new_image, 80);
	}
}