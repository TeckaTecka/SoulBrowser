<?php
class Admin_Form_Products_Products_Parameters extends Zend_Form
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
	        'label'			=>	'Parametr',
    		'required'		=>	true
    	));
    	
    	$this->addElement('text', 'value', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Hodnota',
    		'description'	=>	'*',
        	'title'			=>	'Hodnota musí být vyplněna',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Hodnota musí být vyplněna'))
				)
			)
		));
													
        $this->addElement('submit', 'saveParameter', array(
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
        				'id'	=>	'form-parameter'
        			)
        		),
        		array('Form')
        	)
        );
    }
}