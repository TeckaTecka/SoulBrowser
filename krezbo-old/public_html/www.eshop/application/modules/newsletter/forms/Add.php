<?php
class Newsletter_Form_Add extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
	);
	
    public $buttonDecorators = array(
    	'ViewHelper',
    	array('HtmlTag', array('class'	=>	'button'))
    );
    								 	   
	public function init()
    {
    	$this->addElement('text', 'title', array(
    		'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Název',
    		'description'	=>	'*',
        	'title'			=>	'Název musí být vyplněn',
        	'class'			=>	'tool-tip',
        	'required'		=>	true,
        	'validators'	=>	array(
    			array(
    				'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'	=>	'Název musí být vyplněn'))
				)
			)
		));
		
		$this->addElement('textarea', 'newsletter', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Novinka',
			'required'		=>	true
		));
        																  		
        $this->addElement('submit', 'newsletterSave', array(
        	'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Uložit'
        ));
    }

	public function loadDefaultDecorators()
    {
    	$this->setDecorators(array(
    		'FormElements',
        	array(
        		'HtmlTag',
        		array(
        			'tag'	=>	'div',
        			'id'	=>	'form-newsletter-add'
        		)
        	),
        	array('Form')
        ));
    }
}