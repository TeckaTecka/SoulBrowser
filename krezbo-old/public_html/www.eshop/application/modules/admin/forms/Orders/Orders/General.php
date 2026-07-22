<?php
class Admin_Form_Orders_Orders_General extends Zend_Form
{
	public $selectDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'select'))
	);   
	public $buttonDecorators = array('ViewHelper');
        								 	   
	public function init()
    {
    	$this->addElement('select', 'status', array(
			'decorators'	=>	$this->selectDecorators,
        	'label'			=>	'Stav objednávky',
			'description'	=>	'*',
        	'size'			=>	1,
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Stav objednávky musí být vybrán'))
				)
			)
		));
    	$this->addElement('select', 'consumption', array(
			'decorators'	=>	$this->selectDecorators,
        	'label'			=>	'Doprava',
			'description'	=>	'*',
        	'size'			=>	1,
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Doprava musí být vybrána'))
				)
			)
		));
		$this->addElement('select', 'payment', array(
			'decorators'	=>	$this->selectDecorators,
        	'label'			=>	'Způsob platby',
			'description'	=>	'*',
        	'size'			=>	1,
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Způsob platby musí být vybrán'))
				)
			)
		));
		
        $this->addElement('submit', 'saveGeneral', array(
			'decorators'	=>	$this->buttonDecorators,
			'label'			=>	'Aktualizovat'
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
        				'id'	=>	'form-odrers-general'
        			)
        		),
        		array('Form')
        	)
        );
    }
}