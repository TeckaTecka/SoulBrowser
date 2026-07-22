<?php
class Admin_Form_Sitemap_Pages_HomePage extends Zend_Form
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
													
        $this->addElement('textarea', 'page', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Text stránky'
		));	

		$this->addElement('submit', 'savePage', array(
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
        				'id'	=>	'form-page'
        			)
        		),
        		array('Form')
        	)
        );
    }
}