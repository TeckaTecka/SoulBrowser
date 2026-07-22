<?php
class Admin_Form_Options_Eshop_Currency extends Zend_Form
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
    	$this->addElement('text', 'currency', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Měna',
    		'description'	=>	'*',
        	'title'			=>	'Měna musí být vyplněna',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Měna musí být vyplněna'))
				)
			)
		));
		
		$this->addElement('submit', 'saveCurrency', array(
			'decorators'	=>	$this->buttonDecorators,
			'label'			=>	'Aktualizovat'
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
        				'id'	=>	'form-options-currency'
        			)
        		),
        		array('Form')
        	)
        );
    }
}