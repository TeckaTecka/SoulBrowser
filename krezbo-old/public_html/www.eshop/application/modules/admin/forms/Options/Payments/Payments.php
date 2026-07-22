<?php
class Admin_Form_Options_Payments_Payments extends Zend_Form
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
    	$this->addElement('text', 'payment', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Způsob platby',
        	'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Způsob platby musí být vyplněn'))
				)
			)
		));
		$this->addElement('text', 'price', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Cena včetně DPH',
        	//'description'	=>	'*',
        	//'required'		=>	true,
       		'validators'	=>	array(
    			array(
					'Digits',
					true,
					array(
						'messages'	=>	array('notDigits'	=>	'"%value%" není platná cena. Formát ceny např.: "125"')
					)
				),
				array(
					'StringLength',
					true,
					array(
						0,
						3,
						'UTF-8',
						'messages'	=>	array(
							'stringLengthTooLong'	=>	'Text je delší než %max% znaků.',
							'stringLengthTooShort'	=>	'x',
							'stringLengthInvalid'	=>	'xx'
						)
					)
				)
			)
		));
        $this->addElement('submit', 'savePayments', array(
			'decorators'	=>	$this->buttonDecorators,
			'label'			=>	'OK'
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
        				'id'	=>	'form-countries'
        			)
        		),
        		array('Form')
        	)
        );
    }
}