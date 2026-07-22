<?php
class Admin_Form_Options_Eshop_Company extends Zend_Form
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
    	$this->addElement('text', 'registration', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Registrace',
    		'description'	=>	'*',
        	'title'			=>	'Registrace musí být vyplněna',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Registrace musí být vyplněna'))
				)
			)
		));
		
		$this->addElement('submit', 'saveCompany', array(
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
        				'id'	=>	'form-options-company'
        			)
        		),
        		array('Form')
        	)
        );
    }
}