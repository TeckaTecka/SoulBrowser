<?php
class Auth_Form_Address_Del extends Zend_Form
{
	private $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator' => '')), 
		array(
			'Description',
			array('tag'		=>	'span')
		),
		'Errors',
		array(
			'HtmlTag', array(
				'tag'	=>	'div',
				'class'	=>	'element-form'
			)
		)
	);
	
	private $buttonDecorators = array('ViewHelper');
    
	public function init()
    {
    	/* BUTTONS *******************************************************************************/
		$this->addElement('submit', 'addressDelStorno', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Storno'
		));
		
		$this->addElement('submit', 'addressDelSubmit', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Smazat'
		));
		
		$this->addDisplayGroup(array(
			'addressDelStorno',
        	'addressDelSubmit'),
        'buttons');
        
        $this->getDisplayGroup('buttons')->setDecorators(array(
        	'FormElements',
			array(
				array(
					'data' => 'HtmlTag'
				),
				array(
					'tag' => 'div',
					'class' => 'buttons'
				)
			)
		));
		/*****************************************************************************************/
	}

    public function loadDefaultDecorators()
    {
    	$this->setDecorators(array(
    		'FormElements',
        	array(
        		'HtmlTag',
        		array(
        			'tag'	=>	'div',
        			'id'	=>	'form-del-address',
        			'style'	=>	'width: 200px; margin: auto;'
        		)
        	),
        	array('Form')
        ));
    }
}