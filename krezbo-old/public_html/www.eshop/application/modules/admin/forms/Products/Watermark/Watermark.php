<?php
class Admin_Form_Products_Watermark_Watermark extends Zend_Form
{
	public $buttonDecorators = array('ViewHelper');
   
	public $fileDecorators = array(
		'File',
			array('Label', array('separator'	=>	'')),
			array('Description', array('tag'	=>	'span')),
			
			array(array('row'=>'HtmlTag'), array('tag'=>'div', 'class'	=>	'element-file')),
		'Errors'
	
	);
	
	public function init()
    {
    	$this->addElement('file', 'watermark', array(
    		'decorators'	=>	$this->fileDecorators,
			'label'			=>	'Vyberte obrázek',
			'description'	=>	'*.png',
    		'destination'	=>	'data/png/watermark',
    		//'multifile'		=>	3,
    		'size'			=>	30,
    		'required'		=>	true,
			'validators'	=>	array(
				/*array(
					'Count',
					true,
					array(
						'min'	=>	1,
						'max'	=>	3
					)
    			),*/
    			array(
    				'NotEmpty',
        			true,
        			array('messages'	=>	array('isEmpty'	=>	'Obrázek musí být vybrán'))
        		),
    			array(// 500KB
    				'Size',
    				true,
    				512000
    			),
    			array(
    				'Extension',
    				true,
    				array(
    					'png',
    					'messages'	=>	array('fileExtensionFalse'	=>	"Soubor '%value%' není obrázek typu *.png")
    				)
    			),
    			/*array(
    				'ImageSize',
        			true,
        			array(
        				'minwidth'	=>	500,
        				'maxwidth'	=>	1024,
        				'minheight'	=>	500,
        				'maxheight'	=>	1024,
        				'messages'=>array(
        					'fileImageSizeNotDetected'=>"Velikost obrázku '%value%' nelze detekovat",
        					'fileImageSizeWidthTooBig'=>"Maximální povolená šířka pro obrázek je '%maxwidth%'px, ale obrázek '%value%' má šířku '%width%'px",
        					'fileImageSizeWidthTooSmall'=>"Minimální povolená šířka pro obrázek je '%minwidth%'px, ale obrázek '%value%' má šířku jen '%width%'px",
        					'fileImageSizeHeightTooBig'=>"Maximální povolená výška pro obrázek je '%maxheight%'px, ale obrázek '%value%' má výšku '%height%'px",
        					'fileImageSizeHeightTooSmall'=>"Minimální­ povolená výška pro obrázek je '%minheight%'px, ale obrázek '%value%' má výšku '%height%'px",
        					'fileImageSizeNotReadable'=>"Soubor '%value%' nelze načíst"
        				)
        			)
        		)*/
			)
		));
		$this->addElement('submit', 'saveWatermark', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Uložit',
		));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(
    		array(
    			'FormElements',
        		array(
        			'HtmlTag',
        			array(
        				'tag'	=>	'div',
        				'id'	=>	'form-watermark'
        			)
        		),
        		array('Form')
        	)
        );
    }
}