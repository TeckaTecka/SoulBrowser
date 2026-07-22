<?php
class Admin_Form_Products_Vat_Vat extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
	);
	
    public $buttonDecorators = array('ViewHelper');
    
    public function init()
    {
    	$this->addElement('text', 'vat', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'DPH v %',
    		'description'	=>	'* Cena bez DPH se dopočítává automaticky',
        	'title'			=>	'DPH musí být vyplněno<br />Zadejte číslo od 1 do 100',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'DPH musí být vyplněno'))
				),
				array(
					'Digits',
					true,
					array('messages'	=>	array(
						'notDigits'			=>	"DPH může obsahovat pouze čísla",
						'digitsStringEmpty'	=>	"DPH musí být vyplněno"
					))
				),
				array(
					'Between',
					true,
					array(
						0,
						100,
						'messages'	=>	array(
							'notBetween'		=>	"'%value%' není mezi hodnotami '%min%' až '%max%'",
							'notBetweenStrict'	=>	"'%value%' není striktně mezi hodnotami '%min%' až '%max%'"
						)
					)
				)
			)
		));
													
        $this->addElement('submit', 'saveVat', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Uložit'
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
        				'id'	=>	'form-parameter'
        			)
        		),
        		array('Form')
        	)
        );
    }
}