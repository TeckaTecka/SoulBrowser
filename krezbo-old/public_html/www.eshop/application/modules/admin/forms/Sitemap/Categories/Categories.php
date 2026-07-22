<?php
class Admin_Form_Sitemap_Categories_Categories extends Zend_Form
{
	public $elementDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'element'))
	);
	
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

	public $selectDecorators = array(
		'ViewHelper',
		array('Label', array('separator'	=>	'')), 
		array('Description', array('tag'	=>	'span')),
		'Errors',
		array('HtmlTag', array('tag'	=>	'div', 'class'	=>	'select'))
	);
	
	public function init()
    {
    	/*$this->addElement('select', 'sort_by', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'Zařadit pod',
    		'required'		=>	true
    	));
    	
    	$this->addElement('select', 'order', array(
    		'decorators'	=>	$this->selectDecorators,
	        'label'			=>	'Pořadí',
    		'required'		=>	true
    	));
    	*/
        $this->addElement('text', 'title', array(
        	'decorators'	=>	$this->elementDecorators,
        	'label'			=>	'Název',
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
													
        $this->addElement('text', 'title_menu', array(
        	'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Název v menu',
        	'title'			=>	'Název stránky, který se objevuje např. v drobečkové navigaci.<br />'.
        						'Někdy je praktičtější použít kratší název než je název stránky.<br />'.
        						'Pokud necháte políčko <b>prázdné</b>,<br />'.
        						'vyplní se <b>automaticky</b> podle názvu',
	        'class'			=>	'tool-tip'
        ));
        
		$this->addElement('text', 'title_url', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'URL adresa',
			'title'			=>	'Část adresy, na které se stránka nachází.<br />'.
								'Je důležitá pro vyhledávače.<br />'.
								'Celá adresa je vždy www.obchod.cz/url-kategorie/.<br />'.
								'Pokud necháte políčko <b>prázdné</b>,<br />'.
								'vyplní se <b>automaticky</b> podle názvu',
	        'class'			=>	'tool-tip'
		));
		
		$this->addElement('textarea', 'description', array(
			'decorators'	=>	$this->elementDecorators,
	        'label'			=>	'Popis kategorie'
		));	
		       					  			    					
		$this->addElement('submit', 'saveCategories', array(
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
        				'id'	=>	'form-categories'
        			)
        		),
        		array('Form')
        	)
        );
    }
}