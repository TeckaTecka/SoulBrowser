<?php
class Eshop_Model_DbTable_Settings extends Zend_Db_Table_Abstract
{
	protected $_name = 'settings';
	protected $_primary = 'id';
	/**
     * Return array of Settings flag
     * @param	string	$flag
     * @return	array
     */
	public function getFlag($flag)
	{
		$select = $this->select();
		$select->where('flag = ?', $flag);
		$result = $this->fetchAll($select);
		if ($result){
			$data = array();
			for ($i=0;$i<count($result);$i++) {
				$data[$result[$i]['label']] = $result[$i]['value'];
			}
			return $data;
		}
		return NULL;
	}
}