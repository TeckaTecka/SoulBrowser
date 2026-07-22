<?php
class Admin_Form_Options_Countries_Countries extends Zend_Form
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
    	$this->addElement('text', 'country', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Název země',
        	'description'	=>	'*',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Název musí být vyplněn'))
				)
			)
		));
		
		$this->addElement('submit', 'saveCountries', array(
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