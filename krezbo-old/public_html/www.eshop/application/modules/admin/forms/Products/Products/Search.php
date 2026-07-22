<?php
class Admin_Form_Products_Products_Search extends Zend_Form
{
	
    public $buttonDecorators = array('ViewHelper');
    public $elementDecorators = array('ViewHelper'); 
	public function init()
    {
    	$this->addElement('text', 'search_text', array(
    		'decorators'	=>	$this->elementDecorators,
        	//'label'			=>	'Hledat'
		));
												  
		$this->addElement('submit', 'search', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Hledat'
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
        				'id'	=>	'form-search'
        			)
        		),
        		array('Form')
        	)
        );
    }
}