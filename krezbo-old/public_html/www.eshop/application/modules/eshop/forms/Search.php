<?php
class Eshop_Form_Search extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Errors', array('class'	=>	'errors')),
		'Label',
	);
	
    public $buttonDecorators = array('ViewHelper');
    								 	   
	public function init()
    {
        $this->addElement('text', 'text_search', array(
    		'decorators'	=>	$this->elementDecorators,
        	'placeholder'	=>	'Vyhledat ...',
        	'required'		=>	true
		));
		
        $this->addElement('submit', 'submit_search', array(
        	'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	''
        ));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(
    		array(
    			'FormElements',
        		array('Form')
        	)
        );
    }
}