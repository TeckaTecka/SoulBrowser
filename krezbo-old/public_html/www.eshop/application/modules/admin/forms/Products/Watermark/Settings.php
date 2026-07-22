<?php
class Admin_Form_Products_Watermark_Settings extends Zend_Form
{
	
    public $buttonDecorators = array('ViewHelper');
    public $checkboxDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')),
		array('Description', array('tag'	=>	'span')),
		array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'checkbox'
			)
		)
	);
	 
	public function init()
    {
    	$this->addElement('checkbox', 'enable', array(
    		'decorators'	=>	$this->checkboxDecorators,
			'label'			=>	'Povolit vodoznak'
    	));

		$this->addElement('submit', 'saveSettings', array(
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
        				'id'	=>	'form-settings'
        			)
        		),
        		array('Form')
        	)
        );
    }
}