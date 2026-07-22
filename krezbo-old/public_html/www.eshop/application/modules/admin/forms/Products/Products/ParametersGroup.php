<?php
class Admin_Form_Products_Products_ParametersGroup extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
	);
	
    public $buttonDecorators = array('ViewHelper');
    
    public $selectDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'select'))
	);   
	
	public function init()
    {
    	$this->addElement('select', 'groups', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'Skupina parametrů',
    		'required'		=>	true
    	));
    	
    	$this->addElement('submit', 'setGroup', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Přidat'
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
        				'id'	=>	'form-parameter-group'
        			)
        		),
        		array('Form')
        	)
        );
    }
}