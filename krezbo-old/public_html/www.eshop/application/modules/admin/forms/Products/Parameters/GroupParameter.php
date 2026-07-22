<?php
class Admin_Form_Products_Parameters_GroupParameter extends Zend_Form
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
    	$this->addElement('select', 'parameters', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'Vyberte parametr',
    		'description'	=>	'*',
        	'title'			=>	'Parametr musí být vybrán',
        	'class'			=>	'tool-tip',
    		'required'		=>	true
    	));
    														
        $this->addElement('submit', 'saveParameter', array(
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
        				'id'	=>	'form-parameter-group-add'
        			)
        		),
        		array('Form')
        	)
        );
    }
}