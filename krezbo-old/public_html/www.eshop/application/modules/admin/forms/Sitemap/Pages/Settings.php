<?php
class Admin_Form_Sitemap_Pages_Settings extends Zend_Form
{
	public $buttonDecorators = array('ViewHelper');
    public $checkboxDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')), 
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
							  			    					
		$this->addElement('submit', 'saveSettings', array(
			'decorators'	=>	$this->buttonDecorators,
			'label'			=>	'OK'
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