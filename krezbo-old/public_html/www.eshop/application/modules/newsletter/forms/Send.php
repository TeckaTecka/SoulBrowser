<?php
class Newsletter_Form_Send extends Zend_Form
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
    	$this->addElement('checkbox', 'consumers', array(
    		'decorators'	=>	$this->checkboxDecorators,
			'label'			=>	'Všem přihlášeným odběratelům'
    	));

		$this->addElement('submit', 'sendNewsletters', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Rozeslat'
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