<?php
class Cart_Form_ConsumptionPayment extends Zend_Form
{
	public $elementDecorators = array(
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
	
    public $buttonDecorators = array('ViewHelper');
    
	
	
    public function init()
    {
    	$this->addElement('select', 'consumption', array(
			'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Způsob odběru zboží',
			'description'	=>	'*',
        	'size'			=>	1,
			'required'		=>	true,
			'validators'	=>	array(
				array(
					'NotEmpty',
					true,
					array('messages'	=>	array('isEmpty'		=>	'Způsob odběru zboží musí být vybrán'))
				),
				
			)
		));
		$this->addElement('select', 'payment', array(
			'decorators'	=>	$this->elementDecorators,
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
		$this->addDisplayGroup(array(
			'consumption',
        	'payment'),
        'consumptionPayment');
        
        $this->getDisplayGroup('consumptionPayment')->setDecorators(array(
        	'FormElements',
			array(
				array(
					'data' => 'HtmlTag'
				),
				array(
					'tag' => 'div',
					'class' => 'consumptionPayment'
				)
			)
		));
    	
		/* BUTTONS *******************************************************************************/
		$this->addElement('submit', 'prev', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Zpět'
		));
		
		$this->addElement('submit', 'submit', array(
			'decorators'	=>	$this->buttonDecorators,
        	'label'			=>	'Pokračovat'
		));
		
		$this->addDisplayGroup(array(
			'prev',
        	'submit'),
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
        			'id'	=>	'form-consumption-payment'
        		)
        	),
        	array('Form')
        ));
    }
}