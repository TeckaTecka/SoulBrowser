<?php
class Admin_Form_Options_Countries_Del extends Zend_Form
{
	public $buttonDecorators = array('ViewHelper');
    								 	   
	public function init()
    {
    	$this->addElement('submit', 'delete', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Smazat'
		));
        
        $this->addElement('submit', 'storno', array(
        	'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Storno'
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
        				'id'	=>	'form-del',
        				'style'	=>	'width: 230px; margin: auto;'
        			)
        		),
        		array('Form')
        	)
        );
    }
}