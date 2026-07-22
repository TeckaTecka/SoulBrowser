<?php
class Admin_Form_Options_Eshop_Smtp extends Zend_Form
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
    	$this->addElement('text', 'username', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Uživatelské jméno',
    		'description'	=>	'*',
        	'title'			=>	'Uživatelské jméno musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Uživatelské jméno musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'password', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Heslo',
    		'description'	=>	'*',
        	'title'			=>	'Heslo musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Heslo musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'ssl', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'SSL',
    		'description'	=>	'*',
        	'title'			=>	'SSL musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'SSL musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('text', 'port', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Port',
    		'description'	=>	'*',
        	'title'			=>	'Port musí být vyplněn',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Port musí být vyplněn'))
				)
			)
		));
		
		$this->addElement('text', 'smtp', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'SMTP',
    		'description'	=>	'*',
        	'title'			=>	'SMTP musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'SMTP musí být vyplněno'))
				)
			)
		));
		
        $this->addElement('submit', 'saveSMTP', array(
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
        				'id'	=>	'form-options-smtp'
        			)
        		),
        		array('Form')
        	)
        );
    }
}