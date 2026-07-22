<?php
class Admin_Form_Products_Products_Categories extends Zend_Form
{
	public $buttonDecorators = array('ViewHelper');
    public $checkboxDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')), 
		array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'multicheckbox'
			)
		)
	);
	 
	public function init()
    {
    	$this->addElement('MultiCheckbox', 'categories', array(
    		'decorators'	=>	$this->checkboxDecorators,
        ));
    	
    	$this->addElement('submit', 'saveCategories', array(
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
        				'id'	=>	'form-categories'
        			)
        		),
        		array('Form')
        	)
        );
    }
}