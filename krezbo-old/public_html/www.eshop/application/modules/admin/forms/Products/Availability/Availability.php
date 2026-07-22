<?php
class Admin_Form_Products_Availability_Availability extends Zend_Form
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
    	$this->addElement('text', 'availability', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Dostupnost',
    		'description'	=>	'*',
        	'title'			=>	'Dostupnost musí být vyplněna',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Dostupnost musí být vyplněna'))
				)
			)
		));
													
        $this->addElement('submit', 'saveAvailability', array(
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
        				'id'	=>	'form-availability'
        			)
        		),
        		array('Form')
        	)
        );
    }
}