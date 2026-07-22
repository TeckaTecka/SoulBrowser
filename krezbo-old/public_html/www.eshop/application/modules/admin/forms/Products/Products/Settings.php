<?php
class Admin_Form_Products_Products_Settings extends Zend_Form
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
    	$this->addElement('checkbox', 'show', array(
    		'decorators'	=>	$this->checkboxDecorators,
			'label'			=>	'Zobrazit'
    	));

		$this->addElement('checkbox', 'recommend', array(
			'decorators'	=>	$this->checkboxDecorators,
			'label'			=>	'Doporučujeme',
			'description'	=>	'Zobrazí se na hlavní stránce v sekci "Doporučujeme"'
		));
		
		$this->addElement('checkbox', 'news', array(
			'decorators'	=>	$this->checkboxDecorators,
			'label'			=>	'Novinka',
			'description'	=>	'Zobrazí se na hlavní stránce v sekci "Novinky"'
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