<?php
class Admin_Form_Options_Eshop_Bank extends Zend_Form
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
    	$this->addElement('text', 'account', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Číslo účtu',
    		'description'	=>	'*',
        	'title'			=>	'Číslo účtu musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Číslo účtu musí být vyplněno'))
				)
			)
		));
		$this->addElement('text', 'title', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Název banky',
    		'description'	=>	'*',
        	'title'			=>	'Název banky musí být vyplněno',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Název banky musí být vyplněno'))
				)
			)
		));
		
    	$this->addElement('text', 'iban', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'IBAN',
        	'description'	=>	'*',
    		'title'			=>	'IBAN musí být vyplněn',
        	'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'IBAN musí být vyplněn'))
				)
			)
		));
		
		$this->addElement('text', 'bic', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'BIC',
        	'description'	=>	'*',
			'title'			=>	'BIC musí být vyplněno',
        	'class'			=>	'tool-tip',
			'required'		=>	true,
			'validators'	=>	array(
        		array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'BIC popisné musí být vyplněno'))
				)
			)
		));
		
		$this->addElement('submit', 'saveBank', array(
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
        				'id'	=>	'form-option-bank'
        			)
        		),
        		array('Form')
        	)
        );
    }
}