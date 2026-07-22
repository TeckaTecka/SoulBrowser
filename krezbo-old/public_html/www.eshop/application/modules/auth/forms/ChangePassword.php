<?php
class Auth_Form_ChangePassword extends Zend_Form
{
	public $elementDecorators = array('ViewHelper',
									  array('Errors', array('class'=>'panel-errors')),
									  'Label',
									  array(array('row' => 'HtmlTag'),
							  			    array('tag' => 'div'))
							  		  );
	
    public $buttonDecorators = array('ViewHelper');

    public function init()
    {
        
    	$this->addElement('password', 'password', array('decorators'	=>	$this->elementDecorators,
        												   'label'		=>	'Nové heslo',
        												   'required'	=>	true,
        												   'rows'		=>	5,
        												   'cols'		=>	30,
        												   'validators'	=>	array(array('NotEmpty',
															  							 true,
															  							 array('messages'=>array('isEmpty'=>'Heslo musí být vyplněno'))
															  					   		),
																				   array('Alnum',
															  							 true,
															  							 array('messages'=>array('notAlnum'=>"'%value%' může obsahovat pouze písmena a čísla"))
															  				 			),
															  				 	   array('stringLength',
															  							 true,
															  							 array(array('min'=>6, 'max'=>20),
															  							 	   'messages'=>array('stringLengthTooShort'=>"'%value%' je kratší než %min% znaků",
															  							 						 'stringLengthTooLong'=>"'%value%' je delší než %max% znaků")
															  							 	  )
															  				 			)
															  				 	  )
															  				 ));
		
		$pass = Zend_Controller_Front::getInstance()->getRequest()->getParam('password', '');
		$this->addElement('password', 'password2', array('decorators'	=>	$this->elementDecorators,
        												   'label'		=>	'Znovu nové heslo',
        												   'required'	=>	true,
        												   'rows'		=>	5,
        												   'cols'		=>	30,
        												   'validators'	=>	array(array('NotEmpty',
															  							 true,
															  							 array('messages'=>array('isEmpty'=>'Heslo musí být vyplněno'))
															  					   		),
																				   array('Identical',
															  							  true,
															  							  array($pass, 'messages'=>array('notSame'=>'Heslo se nezhoduje'))
															  					   		 )
				  				 	   
															  				 	  )
															  				 ));
        
        $this->addElement('submit', 'changePswd', array('decorators'	=>	$this->buttonDecorators,
        										  	'label'			=>	'Změnit'));
    }

public function loadDefaultDecorators()
    {
    	$this->setDecorators(array('FormElements',
        						   array('HtmlTag',
        						   		 array('tag' => 'div', 'id' => 'form-auth-changePswd')),//, 'style'=>'width: 100%;')),
        						   array('Form'),));
    }
}